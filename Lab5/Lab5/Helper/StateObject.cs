using System.Collections.Generic;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;

namespace Lab5.Helper
{
    public class StateObject
    {
        public Socket socket;
        public const int BUFFER_SIZE = 512;
        public byte[] buffer;
        public StringBuilder responseContent;
        public int id;
        public string hostname;
        public string endpoint;
        public IPEndPoint remoteEndpoint;

        public StateObject(Socket socket, string hostname, string endpoint, IPEndPoint remoteEndpoint, int id)
        {
            this.socket = socket;
            this.hostname = hostname;
            this.endpoint = endpoint;
            this.remoteEndpoint = remoteEndpoint;
            this.id = id;
            this.buffer = new byte[BUFFER_SIZE];
            this.responseContent = new StringBuilder();
        }
    }
}