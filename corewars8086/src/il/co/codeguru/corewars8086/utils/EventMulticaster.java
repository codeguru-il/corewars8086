package il.co.codeguru.corewars8086.utils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * An event multicaster which broadcasts Events to a number of listeners.
 * @author BS
 */
public class EventMulticaster {
    
    private Class mListenerInterface;
    private EventListener mProxy;
    private Set<EventListener> mListeners = new HashSet<EventListener>();
    private Set<EventListener> mWaitingListeners = new HashSet<EventListener>();
    private boolean isCasting;

    /** Construct a new EventMulticaster for the given listener class.
     */
    public EventMulticaster(Class pListenerInterface) {
        if (! EventListener.class.isAssignableFrom(pListenerInterface) ) {
            throw new IllegalArgumentException("Listener interface must extend java.util.EventListener.");
        }
        mListenerInterface = pListenerInterface;
    }
    
    /** Add an event listener to the list.
     */
    public void add(EventListener pListener) {
    	if (isCasting) {
    		mWaitingListeners .add(pListener);
    	} else {
    		mListeners.add(pListener);
    	}
    }
    
    /** Remove an event listener from the list.
     */
    public void remove(EventListener pListener) {
        mListeners.remove(pListener);
    }

    /** Get the proxy for this event multicaster. This proxy can then be
     * used to broadcast events to all the registered event listeners.
     */
    public synchronized EventListener getProxy() {
        if ( mProxy == null ) {
            ClassLoader lCL = this.getClass().getClassLoader();
            Class[] lClasses = new Class[] {mListenerInterface};
            mProxy = (EventListener)Proxy.newProxyInstance(lCL,lClasses,new MulticasterInvocationHandler());
        }
        return mProxy;
    }
    
    /** Invokes the given method on each listener registered with the
     * multicaster.
     */
    private class MulticasterInvocationHandler implements InvocationHandler {
        public Object invoke(Object pProxy, Method pMethod, Object[] pArgs)
            throws Throwable {
            
            isCasting = true;
            Iterator lListeners = mListeners.iterator();
            while ( lListeners.hasNext() ) {
                pMethod.invoke(lListeners.next(),pArgs);
            }
            isCasting = false;
            if (mWaitingListeners.size() != 0) {
            	// listeners are waiting to be added, add them after multicasting
            	for (EventListener lis: mWaitingListeners) {
            		add(lis);
            	}
            	mWaitingListeners.clear();
            }
            return null;
        }
    } 
}