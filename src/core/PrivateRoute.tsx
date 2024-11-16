import React from 'react';
import { Route, Redirect } from 'react-router-dom';

const PrivateRoute = ({ element, isAuthenticated, ...rest }: any) => {
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