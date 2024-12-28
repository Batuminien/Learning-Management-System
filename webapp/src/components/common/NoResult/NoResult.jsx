import React from 'react';
import { SearchX } from 'lucide-react';
import './NoResult.css';

export default function NoResult() {
  return (
    <div className="no-result-container">
        <SearchX className="no-result-icon" size={48} />
        <p className="no-result-text">No results found</p>
    </div>
  );
}
