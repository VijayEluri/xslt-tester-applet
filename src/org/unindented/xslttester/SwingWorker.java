package org.unindented.xslttester;

import javax.swing.SwingUtilities;

/**
 * This is the 3rd version of SwingWorker (also known as SwingWorker 3), an
 * abstract class that you subclass to perform GUI-related work in a dedicated
 * thread. For instructions on using this class, see:
 *
 * http://java.sun.com/docs/books/tutorial/uiswing/misc/threads.html
 *
 * Note that the API changed slightly in the 3rd version: You must now invoke
 * {@link SwingWorker#start()} on the SwingWorker after creating it.
 */
public abstract class SwingWorker
{
    private Object value; // see getValue(), setValue()

    private static class ThreadVar
    {
        private Thread thread;

        ThreadVar(final Thread t)
        {
            thread = t;
        }

        synchronized Thread get()
        {
            return thread;
        }

        synchronized void clear()
        {
            thread = null;
        }
    }

    private final ThreadVar threadVar;

    protected synchronized Object getValue()
    {
        return value;
    }

    private synchronized void setValue(final Object x)
    {
        value = x;
    }

    public abstract Object construct();

    public void finished()
    {
    }

    public void interrupt()
    {
        Thread t = threadVar.get();
        if (t != null)
        {
            t.interrupt();
        }
        threadVar.clear();
    }

    public Object get()
    {
        while (true)
        {
            Thread t = threadVar.get();
            if (t == null)
            {
                return getValue();
            }
            try
            {
                t.join();
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt(); // propagate
                return null;
            }
        }
    }

    public SwingWorker()
    {
        final Runnable doFinished = new Runnable()
        {
            public void run()
            {
                finished();
            }
        };

        Runnable doConstruct = new Runnable()
        {
            public void run()
            {
                try
                {
                    setValue(construct());
                }
                finally
                {
                    threadVar.clear();
                }

                SwingUtilities.invokeLater(doFinished);
            }
        };

        Thread t = new Thread(doConstruct);
        threadVar = new ThreadVar(t);
    }

    public void start()
    {
        Thread t = threadVar.get();
        if (t != null)
        {
            t.start();
        }
    }
}
