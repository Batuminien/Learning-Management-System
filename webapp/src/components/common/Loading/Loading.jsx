import React from 'react';
import { Loader2 } from 'lucide-react';
import './Loading.css';

const Loading = () => {
    return(
        <div className="icon-component-container">
            <Loader2 className="loading-spinner" />
            <p className="icon-component-text">Loading</p>
        </div>
    );
}
export default Loading;