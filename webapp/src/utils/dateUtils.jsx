
/**
 * Checks if the given date string is in the future.
 * @param {string} dateString - The date string to check.
 * @returns {boolean} - Returns true if the date is in the future, false otherwise.
 */
export const isDateInFuture = (dateString) => {
    const inputDate = new Date(dateString);
    const currentDate = new Date();
    
    // Clear the time part of the current date for comparison
    currentDate.setHours(23, 59, 59, 0);
    
    return inputDate > currentDate;
};

export const gettwoyearsBefore = () => {
    const currentDate = new Date();
    currentDate.setFullYear(currentDate.getFullYear() - 2);
    const twoYearsAgo = currentDate.toISOString().split('T')[0];
    return twoYearsAgo;
}

export const getPreviousDay = (dateString) => {
    // Parse the input date string into a Date object
    const givenDate = new Date(dateString);

    // Subtract two days (in milliseconds)
    const twoDaysBefore = new Date(givenDate);
    twoDaysBefore.setDate(givenDate.getDate() - 1);

    // Format the result as yyyy-mm-dd
    const year = twoDaysBefore.getFullYear();
    const month = String(twoDaysBefore.getMonth() + 1).padStart(2, '0');
    const day = String(twoDaysBefore.getDate()).padStart(2, '0');

    return `${year}-${month}-${day}`;
}

export const areDatesEqual = (date1, date2) => {
    const d1 = new Date(date1);
    const d2 = new Date(date2);

    return (
        d1.getFullYear() === d2.getFullYear() &&
        d1.getMonth() === d2.getMonth() &&
        d1.getDate() === d2.getDate()
    );
}