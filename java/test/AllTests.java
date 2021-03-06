// **********************************************************************
//
// Copyright (c) 2003-2017 ZeroC, Inc. All rights reserved.
//
// **********************************************************************

public class AllTests
{
    private static void
    test(boolean b)
    {
        if(!b)
        {
            throw new RuntimeException();
        }
    }

    private static class Callback
    {
        Callback()
        {
            _called = false;
        }

        public synchronized void
        check()
        {
            while(!_called)
            {
                try
                {
                    wait();
                }
                catch(InterruptedException ex)
                {
                }
            }

            _called = false;
        }

        public synchronized void
        called()
        {
            assert(!_called);
            _called = true;
            notify();
        }

        private boolean _called;
    }

    private static class opMessageI extends ProtobufTest.Callback_MyClass_opMessage
    {
        @Override
        public void response(test.TestPB.Message o, test.TestPB.Message r)
        {
            test(o.getI() == 99);
            test(r.getI() == 99);
            callback.called();
        }

        @Override
        public void exception(Ice.LocalException ex)
        {
            test(false);
        }

        public void check()
        {
            callback.check();
        }

        private Callback callback = new Callback();
    }

    private static class opMessageAMDI extends ProtobufTest.Callback_MyClass_opMessageAMD
    {
        @Override
        public void response(test.TestPB.Message o, test.TestPB.Message r)
        {
            test(o.getI() == 99);
            test(r.getI() == 99);
            callback.called();
        }

        @Override
        public void exception(Ice.LocalException ex)
        {
            test(false);
        }

        public void check()
        {
            callback.check();
        }

        private Callback callback = new Callback();
    }

    public static ProtobufTest.MyClassPrx
    allTests(Ice.Communicator communicator, boolean collocated)
    {
        String ref = "test:default -p 12010";
        Ice.ObjectPrx baseProxy = communicator.stringToProxy(ref);
        ProtobufTest.MyClassPrx cl = ProtobufTest.MyClassPrxHelper.checkedCast(baseProxy);

        System.out.print("testing twoway operations... ");
        {
            test.TestPB.Message i = test.TestPB.Message.newBuilder().setI(99).build();

            Ice.Holder<test.TestPB.Message> o = new Ice.Holder<test.TestPB.Message>();
            test.TestPB.Message r;

            r = cl.opMessage(i, o);

            test(o.value.getI() == 99);
            test(r.getI() == 99);
        }
        {
            test.TestPB.Message i = test.TestPB.Message.newBuilder().setI(99).build();
            Ice.Holder<test.TestPB.Message> o = new Ice.Holder<test.TestPB.Message>();
            test.TestPB.Message r;

            r = cl.opMessageAMD(i, o);

            test(o.value.getI() == 99);
            test(r.getI() == 99);
        }
        System.out.println("ok");

        System.out.print("testing twoway AMI operations... ");
        {
            test.TestPB.Message i = test.TestPB.Message.newBuilder().setI(99).build();

            opMessageI cb = new opMessageI();
            cl.begin_opMessage(i, cb);
            cb.check();
        }
        {
            test.TestPB.Message i = test.TestPB.Message.newBuilder().setI(99).build();

            opMessageAMDI cb = new opMessageAMDI();
            cl.begin_opMessageAMD(i, cb);
            cb.check();
        }
        System.out.println("ok");

        return cl;
    }
}
