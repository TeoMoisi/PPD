#include <mpi.h>
#include <iostream>
#include <mutex>
#include <vector>
#include <queue>
#include <thread>
#include <unistd.h>
#include <ctime>

using namespace std;

struct ChangeInfo{
	int senderId;
	int variable;
	int value;
	int compare;
};

int procId, processesNumber; 
int memory[5][5];
bool done = false;
queue<ChangeInfo> notifyQueue;
mutex mtx;

vector<ChangeInfo> get_changes(int source, int tag){
	int n;
	MPI_Recv(&n, 1, MPI_INT, source, tag, MPI_COMM_WORLD, MPI_STATUS_IGNORE);

	if (n == 0)
		return vector<ChangeInfo>();

	vector<ChangeInfo> changes;
	for (int i = 0; i < n; i++){
		ChangeInfo changeInfo;
		MPI_Status status;
		MPI_Recv(&changeInfo, sizeof(ChangeInfo) / sizeof(int), MPI_INT, source, tag, MPI_COMM_WORLD, &status);
		changes.push_back(changeInfo);
	}
	return changes;
}

void send_changes(vector<ChangeInfo> changes, int tag){
	int n = changes.size();
	MPI_Send(&n, 1, MPI_INT, 0, tag, MPI_COMM_WORLD);

	if (n == 0)
		return;

	for (int i = 0; i < n; i++)
		MPI_Send(&changes[i], sizeof(ChangeInfo) / sizeof(int), MPI_INT, 0, tag, MPI_COMM_WORLD);
}

void broadcast_send(vector<ChangeInfo> changes){
	int n = changes.size();
	MPI_Bcast(&n, 1, MPI_INT, 0, MPI_COMM_WORLD);

	//if there are no changes, return
	if (n == 0)
		return;

  //otherwise, we se the value of the variable into the memory as well
	mtx.lock();
	for (int i = 0; i < changes.size(); i++){
		// cout << changes[i].senderId << " " << changes[i].variable << " " << changes[i].value << "\n" << flush;
		if (changes[i].compare == -1 || changes[i].compare == memory[changes[i].senderId][changes[i].variable])
			memory[changes[i].senderId][changes[i].variable] = changes[i].value;
			//we send every single change separately
		MPI_Bcast(&changes[i], sizeof(ChangeInfo) / sizeof(int), MPI_INT, 0, MPI_COMM_WORLD);
	}
	mtx.unlock();
}

void broadcast_receive(){
	int n;
	MPI_Bcast(&n, 1, MPI_INT, 0, MPI_COMM_WORLD);

	if (n == 0)
		return;

	mtx.lock();
	//we receive every single change separately
	ChangeInfo changeInfo;
	for (int i = 0; i < n; i++){
		MPI_Bcast(&changeInfo, sizeof(ChangeInfo) / sizeof(int), MPI_INT, 0, MPI_COMM_WORLD);
		if (changeInfo.compare == -1 || changeInfo.compare == memory[changeInfo.senderId][changeInfo.variable])
			memory[changeInfo.senderId][changeInfo.variable] = changeInfo.value;
	}
	mtx.unlock();
}

void poll_master(){
	int tag = 0;
	while(!done){
		vector<ChangeInfo> changes;
		mtx.lock();
		while(notifyQueue.size() > 0){
			changes.push_back(notifyQueue.front());
			notifyQueue.pop();
		}
		mtx.unlock();

		//master sends the changes
		broadcast_send(changes);
		//cout << "Master finished broadcasting changes\n" << flush;

		for (int i = 1; i < processesNumber; i++){
			vector<ChangeInfo> changes = get_changes(i, tag);
			if (changes.size() > 0){
				mtx.lock();
				for (int j=0; j<changes.size(); j++)
					notifyQueue.push(changes[j]);
				mtx.unlock();
			}
		}

		//cout << "Master finished a polling cycle\n" << flush;
		tag++;
	}
}

void poll_worker(){
	int tag = 0;
	while(!done){
		//receives the changes
		broadcast_receive();
		//cout << "Worker " << procId << " finished receiveng broadcasted changes\n" << flush;
		vector<ChangeInfo> changes;
		mtx.lock();
		//pushes the changes from the notifyQueue
		while(notifyQueue.size() > 0){
			changes.push_back(notifyQueue.front());
			notifyQueue.pop();
		}
		mtx.unlock();
		send_changes(changes, tag);

		//cout << "Worker " << procId << " finished sending changes\n" << flush;
		tag++;
	}
}

void write_value(int senderId, int variable, int value){
	ChangeInfo changeInfo;
	changeInfo.senderId = senderId;
	changeInfo.variable = variable;
	changeInfo.value = value;
	changeInfo.compare = -1;
	mtx.lock();
	notifyQueue.push(changeInfo);
	mtx.unlock();
}

void compare_exchange(int senderId, int variable, int oldValue, int newValue) {
	ChangeInfo changeInfo;
	changeInfo.senderId = senderId;
	changeInfo.variable = variable;
	changeInfo.value = newValue;
	changeInfo.compare = oldValue;
	mtx.lock();
	notifyQueue.push(changeInfo);
	mtx.unlock();
}

void query_value(int senderId, int variable){
	cout << "[I AM " << procId << "] Process " << senderId << " on variable " << variable << " -> " << memory[senderId][variable] << "\n" << flush;
}

void printAll(int senderId) {
	cout << "[I AM " << procId << "] Process " << senderId << ": ";
	for (int i = 1; i <= processesNumber; i++) {
		cout << "variable" << i << " = " << memory[senderId][i] << ";";
	}
	cout << "\n";
}

void process_0(){
	write_value(1, 2, 220);
	usleep(10000);
	//query_value(1, 2);
	printAll(1);
	//usleep(5000);

	compare_exchange(1, 2, 220, 230);
	usleep(10000);
	printAll(1);
}

void process_1(){
	usleep(10000);
	//printAll(1);
	write_value(1, 3, 100);
	usleep(10000);
	printAll(1);
	usleep(10000);

	compare_exchange(1, 3, 100, 150);
	usleep(10000);
	printAll(1);
}

void process_2(){
	usleep(30000);
	write_value(1, 2, 30);
	usleep(20000);
	printAll(1);
	usleep(20000);

	compare_exchange(1, 2, 30, 40);
	usleep(20000);
	printAll(1);
}

int main(){
	MPI_Init(NULL, NULL);

	MPI_Comm_rank(MPI_COMM_WORLD, &procId);
	MPI_Comm_size(MPI_COMM_WORLD, &processesNumber);

	if (procId == 0){
		thread master(poll_master);
		process_0();
		usleep(2000000);
		cout << "Master done\n"<<flush;
		done = true;
		master.join();
	} else{
		thread worker(poll_worker);
		switch(procId){
			case 1:
				process_1();
				break;
			case 2:
				process_2();
				break;
			default:
				break;
		}
		usleep(2000000);
		cout << "Worker " << procId << " done \n" << flush;
		done = true;
		worker.join();
	}

	MPI_Finalize();
	return 0;
}
