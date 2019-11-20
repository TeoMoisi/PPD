using System;
using System.Collections.Generic;

namespace Lab5
{
    class Program
    {
        static void Main(string[] args)
        {
            var ADDRESSES = new List<string>();
            ADDRESSES.Add("www.cs.ubbcluj.ro/~dan/");
            ADDRESSES.Add("facebook.com");
            ADDRESSES.Add("www.cs.ubbcluj.ro/~rlupsa/edu/pdp");
            //TaskDriven.TaskDriven.run(ADDRESSES);
            AsyncAwait.AsyncAwait.run(ADDRESSES);
            //EventDriven.EventDrivenImpl.run(ADDRESSES);
        }
    }
}