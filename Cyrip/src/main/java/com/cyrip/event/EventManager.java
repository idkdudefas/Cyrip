package com.cyrip.event;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventManager {
    private final Map<Class<? extends Event>, List<EventData>> registeredEvents = new HashMap<>();
    
    public void register(Object object) {
        for (Method method : object.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(EventTarget.class) && method.getParameterCount() == 1) {
                Class<?> paramType = method.getParameterTypes()[0];
                if (Event.class.isAssignableFrom(paramType)) {
                    method.setAccessible(true);
                    
                    @SuppressWarnings("unchecked")
                    Class<? extends Event> eventClass = (Class<? extends Event>) paramType;
                    
                    EventData data = new EventData(object, method, method.getAnnotation(EventTarget.class).priority());
                    
                    if (!registeredEvents.containsKey(eventClass)) {
                        registeredEvents.put(eventClass, new ArrayList<>());
                    }
                    
                    registeredEvents.get(eventClass).add(data);
                    
                    // Sort by priority
                    registeredEvents.get(eventClass).sort((a, b) -> Integer.compare(b.priority, a.priority));
                }
            }
        }
    }
    
    public void unregister(Object object) {
        for (List<EventData> events : registeredEvents.values()) {
            events.removeIf(data -> data.object == object);
        }
    }
    
    public void post(Event event) {
        List<EventData> dataList = registeredEvents.get(event.getClass());
        
        if (dataList != null) {
            for (EventData data : dataList) {
                try {
                    data.method.invoke(data.object, event);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                if (event.isCancelled()) {
                    break;
                }
            }
        }
    }
    
    private static class EventData {
        private final Object object;
        private final Method method;
        private final int priority;
        
        public EventData(Object object, Method method, int priority) {
            this.object = object;
            this.method = method;
            this.priority = priority;
        }
    }
}