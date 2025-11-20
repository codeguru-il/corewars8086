import React from 'react';

// The main Tabs component now manages the state of which tab is active
export const Tabs = ({ className, children, value, onValueChange }) => {
    // We pass the active value and the click handler down to the children
    return (
        <div className={className}>
            {React.Children.map(children, child => {
                if (React.isValidElement(child)) {
                    return React.cloneElement(child, { activeValue: value, onValueChange });
                }
                return child;
            })}
        </div>
    );
};

// The TabsList just renders its children, passing props down
export const TabsList = ({ className, children, activeValue, onValueChange }) => (
    <div className={className}>
        {React.Children.map(children, child => {
            if (React.isValidElement(child)) {
                return React.cloneElement(child, { activeValue, onValueChange });
            }
            return child;
        })}
    </div>
);

// The TabsTrigger is now a real button that handles clicks
export const TabsTrigger = ({ className, children, value, activeValue, onValueChange }) => {
    // Determine if this specific tab is the active one
    const isActive = value === activeValue;
    
    // The 'data-state' attribute is what the original CSS uses to style the active tab
    const activeClassName = isActive ? 'data-[state=active]' : '';

    return (
        <button 
            className={`${className} ${activeClassName}`} 
            data-state={isActive ? 'active' : 'inactive'}
            onClick={() => onValueChange(value)}
        >
            {children}
        </button>
    );
};