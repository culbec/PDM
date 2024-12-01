import React from 'react';
import { Route, Redirect } from 'react-router-dom';

interface PrivateRouteProps {
    element: React.ComponentType<any>;
    isAuthenticated: boolean;
    path: string;
    exact?: boolean;
}

const PrivateRoute: React.FC<PrivateRouteProps> = ({ element, isAuthenticated, ...rest }) => {
    return (
        <Route {...rest} render={(props) => (
            isAuthenticated ? (
                React.createElement(element, props)
            ) : (
                <Redirect to="/gamestop/login" />
            )
        )} />
    );
}

export default PrivateRoute;