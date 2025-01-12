import React, { createContext, useEffect, useState } from "react";

export const AuthContext = createContext();

export const AuthProvider = ({children}) => {
    const [user, setUser] = useState(null);

    useEffect(() => {
        const storedUser = sessionStorage.getItem('user');
        if(storedUser) {
            setUser(JSON.parse(storedUser));
        }
    }, []);

    const login = (userData) => {
        setUser(userData);
        sessionStorage.setItem('user', JSON.stringify(userData));
        sessionStorage.setItem('accessToken', JSON.stringify(userData.accessToken));
    }

    const logout = () => {
        setUser(null);
        sessionStorage.removeItem('user');
        sessionStorage.removeItem('accessToekn');
    }

    return(
        <AuthContext.Provider value={{user, login, logout}}>
            {children}
        </AuthContext.Provider>
    );
};