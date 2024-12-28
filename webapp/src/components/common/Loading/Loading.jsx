import React from 'react';
import { Loader2 } from 'lucide-react';
import './Loading.css';

const Loading = () => {
    return(
        <div className="loading-container">
            <div className="loading-content">
                <Loader2 className="loading-spinner" />
                <p className="loading-text">Loading</p>
            </div>
        </div>
    );
}
export default Loading;