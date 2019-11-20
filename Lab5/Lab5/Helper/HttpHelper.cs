using System;

namespace Lab5.Helper
{
    public class HttpHelper
    {
        public static string getHostName(string host)
        {
            return host.Split('/')[0];
        }

        public static string getEndPoint(string host)
        {
            return host.Contains("/") ? host.Substring(host.IndexOf("/", StringComparison.Ordinal)) : "/";
        }
        public static string parseBodyResponse(string responseContent) {
            var responseParts = responseContent.Split(new[] {"\r\n\r\n"}, StringSplitOptions.RemoveEmptyEntries);

            return responseParts.Length > 1 ? responseParts[1] : "";
        }
        public static int parseHttpResponse(string responseContent) {
            var contentLength = 0;
            var responseLines = responseContent.Split('\r', '\n');

            foreach (var responseLine in responseLines) {
                var headerDetails = responseLine.Split(':');

                if (headerDetails[0].CompareTo("Content-Length") == 0) {
                    contentLength = int.Parse(headerDetails[1]);
                }
            }

            return contentLength;
        }
    }
}