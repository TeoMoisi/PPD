using System;
using System.Collections.Generic;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Lab5.Helper;

namespace Lab5.TaskDriven
{
    public class TaskDriven
    {
        private static List<string> HOST_ADDRESSES;
        private static List<ManualResetEvent> CONNECT_DONE;
        private static List<ManualResetEvent> SENT_DONE;
        private static List<ManualResetEvent> RECEIVED_DONE;

        public static void run(List<string> hostnames) {
            HOST_ADDRESSES = hostnames;

            var tasks = new List<Task>();
            CONNECT_DONE = new List<ManualResetEvent>();
            SENT_DONE = new List<ManualResetEvent>();
            RECEIVED_DONE = new List<ManualResetEvent>();

            for (var i = 0; i < HOST_ADDRESSES.Count; i++) {
                tasks.Add(Task.Factory.StartNew(begin, i));
                CONNECT_DONE.Add(new ManualResetEvent(false));
                SENT_DONE.Add(new ManualResetEvent(false));
                RECEIVED_DONE.Add(new ManualResetEvent(false));
            }

            Task.WaitAll(tasks.ToArray());
        }
        private static void begin(object idObject) {
            var id = (int) idObject;

            ServerConnection(HOST_ADDRESSES[id], id);
        }
        private static void ServerConnection(string host, int id) {
            // establish the remote endpoint of the server  
            var ipHostInfo = Dns.GetHostEntry(host.Split('/')[0]);
            var ipAddress = ipHostInfo.AddressList[0];
            var remoteEndpoint = new IPEndPoint(ipAddress, 80);

            Socket socket = new Socket(ipAddress.AddressFamily, SocketType.Stream, ProtocolType.Tcp);

            string hostname = HttpHelper.getHostName(host);
            string endPoint = HttpHelper.getEndPoint(host);
            StateObject state = new StateObject(socket, hostname, endPoint, remoteEndpoint, id);

            ConnectWrapper(state, id).Wait();
            string data = "GET " + state.endpoint + " HTTP/1.1\r\nHost: " + state.hostname +
                          "\r\nUser - Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_4) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/12.1 Safari/605.1.15\r\nAccept: text / html,application / xhtml + xml,application / xml; q = 0.9,*/*;q=0.8\r\nAccept - Language: en-US,en;q=0.5\r\nAccept - Encoding: gzip, deflate\r\nReferer: http://clipart-library.com/clipart/575061.htm\r\nConnection: keep-alive\r\nUpgrade - Insecure-Requests: 1\r\nIf - Modified-Since: Wed, 01 Mar 2017 15:35:52 GMT\r\nIf - None-Match: \"58b6ea58-12969\"\r\nCache - Control: max-age=0\r\n\r\n";
            SendWrapper(state, data, id).Wait();
            ReceiveWrapper(state, id).Wait();

            Console.WriteLine("Response received by {0} : got {1} total chars", id, state.responseContent.Length);
            socket.Shutdown(SocketShutdown.Both);
            socket.Close();
        }

        private static Task ConnectWrapper(StateObject state, int id) {
            state.socket.BeginConnect(state.remoteEndpoint, ConnectCallback, state);

            return Task.FromResult(CONNECT_DONE[id].WaitOne());
        }

        private static void ConnectCallback(IAsyncResult ar) {
            var state = (StateObject) ar.AsyncState;
            var clientSocket = state.socket;
            var clientId = state.id;
            var hostname = state.hostname;

            clientSocket.EndConnect(ar);
            Console.WriteLine("Socket {0} connected to hostname: {1} , having {2} ip address", clientId, hostname, clientSocket.RemoteEndPoint);
           CONNECT_DONE[clientId].Set();
        }

        private static Task SendWrapper(StateObject state, string data, int id) {
            // ASCII encoding.  
            var byteData = Encoding.ASCII.GetBytes(data);

            state.socket.BeginSend(byteData, 0, byteData.Length, 0, SendCallback, state);
            return Task.FromResult(SENT_DONE[id].WaitOne());
        }

        private static void SendCallback(IAsyncResult ar) {
            var state = (StateObject) ar.AsyncState;
            var clientSocket = state.socket;
            var clientId = state.id;

            // complete sending the data to the server  
            var bytesSent = clientSocket.EndSend(ar);
            Console.WriteLine("Socket {0} sent {1} bytes to server.", clientId, bytesSent);
            SENT_DONE[clientId].Set();
        }

        private static Task ReceiveWrapper(StateObject state, int id) {
            // begin receiving the data from the server
            state.socket.BeginReceive(state.buffer, 0, StateObject.BUFFER_SIZE, 0, ReceiveCallback, state);

            return Task.FromResult(RECEIVED_DONE[id].WaitOne());
        }

        private static void ReceiveCallback(IAsyncResult ar) {
            var state = (StateObject) ar.AsyncState;
            var clientSocket = state.socket;
            var clientId = state.id;

            try {
                // read the data
                var bytesRead = clientSocket.EndReceive(ar);
                // store the characters in the responseContent
                state.responseContent.Append(Encoding.ASCII.GetString(state.buffer, 0, bytesRead));
                //if we have a new empty line
                if (!state.responseContent.ToString().Contains("\r\n\r\n")) {
                    //get the next chunk of data
                    clientSocket.BeginReceive(state.buffer, 0, StateObject.BUFFER_SIZE, 0, ReceiveCallback, state);
                } else {
                    // get the body
                    var responseBody = HttpHelper.parseBodyResponse(state.responseContent.ToString());
                    if (responseBody.Length < HttpHelper.parseHttpResponse(state.responseContent.ToString())) {
                        //retrieve more data
                        clientSocket.BeginReceive(state.buffer, 0, StateObject.BUFFER_SIZE, 0, ReceiveCallback, state);
                    } else {
                        //everything has been received
                        RECEIVED_DONE[clientId].Set();
                    }
                }
            } catch (Exception e) {
                Console.WriteLine(e.ToString());
            }
        }
    }
}