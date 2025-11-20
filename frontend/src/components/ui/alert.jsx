import React from 'react';
export const Alert = ({ className, children }) => <div className={className}>{children}</div>;
export const AlertDescription = ({ className, children }) => <p className={className}>{children}</p>;