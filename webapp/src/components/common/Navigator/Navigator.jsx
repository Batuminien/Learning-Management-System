import { useState } from 'react';
import NavigationOption from '../NavigationOption/NavigationOption';
import './Navigator.css';

const Navigator = ({ options, onSelect, currentOption = 0}) => {

    const [highlightedOption, setHighlightedOption] = useState(currentOption);

    return(
        <div className="navigator">
            {options.map((option, index) => (
                <NavigationOption
                    key={index}
                    title={option.title}
                    IconSource={option?.iconSource}
                    isHighlighted={index === highlightedOption}
                    onClick={() => {
                        setHighlightedOption(index);
                        onSelect(option);
                    }}
                />
            ))}
        </div>
    );
}
export default Navigator;