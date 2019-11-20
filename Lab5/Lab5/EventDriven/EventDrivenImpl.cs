using System;
using System.Collections.Generic;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using Lab5.Helper;

namespace Lab5.EventDriven
{
    public class EventDrivenImpl
    {
        private static List<string> HOST_ADDRESSES;

        public static void run(List<string> hostnames) {
            HOST_ADDRESSES = hostnames;

            for (var i = 0; i < HOST_ADDRESSES.Count; i++) {
                begin(i);
                Thread.Sleep(5000);
            }
        }
        private static void begin(int id) {
            ServerConnection(HOST_ADDRESSES[id], id);
        }
        private static void ServerConnection(string host, int id) {
            var ipHostInfo = Dns.GetHostEntry(host.Split('/')[0]);
            var ipAddress = ipHostInfo.AddressList[0];
            var remoteEndpoint = new IPEndPoint(ipAddress, 80);

            Socket socket = new Socket(ipAddress.AddressFamily, SocketType.Stream, ProtocolType.Tcp);

            string hostname = HttpHelper.getHostName(host);
            string endPoint = HttpHelper.getEndPoint(host);
            StateObject state = new StateObject(socket, hostname, endPoint, remoteEndpoint, id);
            state.socket.BeginConnect(state.remoteEndpoint, Connect, state);
        }

        private static void Connect(IAsyncResult ar) {
            var state = (StateObject) ar.AsyncState;
            var clientSocket = state.socket;
            var clientId = state.id;
            var hostname = state.hostname;

            clientSocket.EndConnect(ar);
            Console.WriteLine("Socket {0} connected to hostname: {1} , having {2} ip address", clientId, hostname, clientSocket.RemoteEndPoint);
            string data = "GET " + state.endpoint + " HTTP/1.1\r\nHost: " + state.hostname +
                          "\r\nUser - Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_4) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/12.1 Safari/605.1.15\r\nAccept: text / html,application / xhtml + xml,application / xml; q = 0.9,*/*;q=0.8\r\nAccept - Language: en-US,en;q=0.5\r\nAccept - Encoding: gzip, deflate\r\nReferer: http://clipart-library.com/clipart/575061.htm\r\nConnection: keep-alive\r\nUpgrade - Insecure-Requests: 1\r\nIf - Modified-Since: Wed, 01 Mar 2017 15:35:52 GMT\r\nIf - None-Match: \"58b6ea58-12969\"\r\nCache - Control: max-age=0\r\n\r\n";
            var byteData = Encoding.ASCII.GetBytes(data);
            state.socket.BeginSend(byteData, 0, byteData.Length, 0, Send, state);
        }

        private static void Send(IAsyncResult ar) {
            var state = (StateObject) ar.AsyncState;
            var clientSocket = state.socket;
            var clientId = state.id;

            var bytesSent = clientSocket.EndSend(ar);
            Console.WriteLine("Socket {0} sent {1} bytes to server.", clientId, bytesSent);
            
            state.socket.BeginReceive(state.buffer, 0, StateObject.BUFFER_SIZE, 0, Receive, state);
        }

        private static void Receive(IAsyncResult ar) {
            var state = (StateObject) ar.AsyncState;
            var clientSocket = state.socket;
            var clientId = state.id;

            try {
                //read the data
                var bytesRead = clientSocket.EndReceive(ar);
                //store the characters from the buffer it in the responseContent
                state.responseContent.Append(Encoding.ASCII.GetString(state.buffer, 0, bytesRead));

                //if we have a new empty line
                if (!state.responseContent.ToString().Contains("\r\n\r\n")) {
                    //get the next chunk of data
                    clientSocket.BeginReceive(state.buffer, 0, StateObject.BUFFER_SIZE, 0, Receive, state);
                } else {
                    // get the body
                    var responseBody = HttpHelper.parseBodyResponse(state.responseContent.ToString());
                    
                    var contentLengthHeaderValue = HttpHelper.parseHttpResponse(state.responseContent.ToString());
                    if (responseBody.Length < contentLengthHeaderValue) {
                        // we retrieve more data
                        clientSocket.BeginReceive(state.buffer, 0, StateObject.BUFFER_SIZE, 0, Receive, state);
                    } else {
                        //we got all the data
                        foreach (var i in state.responseContent.ToString().Split('\r', '\n'))
                            Console.WriteLine(i);
                        Console.WriteLine("Response received by {0} : got {1} total chars", clientId, state.responseContent.Length);
                        clientSocket.Shutdown(SocketShutdown.Both);
                        clientSocket.Close();
                    }
                }
            } catch (Exception e) {
                Console.WriteLine(e.ToString());
            }
        }
    }
}