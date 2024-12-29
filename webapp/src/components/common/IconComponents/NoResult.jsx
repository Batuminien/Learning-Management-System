import React from 'react';
import { NoResultIcon } from '../../../../public/icons/Icons';
import './IconComponents.css';

export default function NoResult() {
  return (
    <div className="icon-component-container">
        <NoResultIcon/>
        <p className="icon-component-text">No results found</p>
    </div>
  );
}
