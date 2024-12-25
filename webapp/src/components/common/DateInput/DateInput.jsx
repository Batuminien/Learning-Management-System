import { useState, useEffect } from "react";
import { isDateInFuture } from "../../../utils/dateUtils";


const DateInput = ({title = 'Tarih', initialDate = '', isFutureInput = false, onInput, errorMessage = ''}) => {

    const [currentDate, setCurrentDate] = useState(initialDate);
    const [dateError, setDateError] = useState(errorMessage);

    useEffect(() => {
        setDateError(errorMessage);
    }, [errorMessage]);

    const handleDateChange = (event) => {
        const newDate = event.target.value;
        setDateError('');
    
        isFutureInput
            ? isDateInFuture(newDate)
                ? (setCurrentDate(newDate), onInput(newDate))
                : (setDateError('Seçilen tarih gelecekte olmalıdır.'), setCurrentDate(initialDate))
            : !isDateInFuture(newDate)
                ? (setCurrentDate(newDate), onInput(newDate))
                : (setDateError('Seçilen tarih geçmişte olmalıdır.'), setCurrentDate(initialDate));
    };
    
    return(
        <div className="input-container">
            <label className="label">{title}</label>
            <input
                className='input'
                type='date'
                value={currentDate}
                onChange={handleDateChange}
            />
            {dateError && <p className='error-message'>{dateError}</p>}    
        </div>
    );
}
export default DateInput;