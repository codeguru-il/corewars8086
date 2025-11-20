import React from 'react';
export const Button = ({ className, children, ...props }) => <button className={className} {...props}>{children}</button>;