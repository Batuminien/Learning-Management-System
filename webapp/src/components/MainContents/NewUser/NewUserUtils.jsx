export const isValidName = (name) => {
    // Ensure the input is a string and meets the minimum length requirement
    if (typeof name !== 'string' || name.length < 3) {
      return false;
    }
  
    // Extended regex to include Turkish characters
    const nameRegex = /^[a-zA-ZğüşöçıĞÜŞÖÇİ]+$/;
    return nameRegex.test(name);
  };

export const isValidTC = (TC) => {
    // Ensure TC is a string to validate properly
    TC = TC.toString();
  
    //Must be 11 digits
    if (!/^\d{11}$/.test(TC)) return false;
  
    //First digit cannot be 0
    if (TC[0] === '0') return false;
  
    // Extract digits as numbers
    const digits = TC.split('').map(Number);
  
    // Calculate the sum of odd and even indexed digits
    const oddSum = digits[0] + digits[2] + digits[4] + digits[6] + digits[8];
    const evenSum = digits[1] + digits[3] + digits[5] + digits[7];
  
    // Validate 10th digit
    const tenthDigit = ((oddSum * 7 - evenSum) % 10 + 10) % 10; // Ensure non-negative modulo
    if (tenthDigit !== digits[9]) return false;
  
    // Validate 11th digit
    const sumOfFirstTen = digits.slice(0, 10).reduce((acc, digit) => acc + digit, 0);
    const eleventhDigit = sumOfFirstTen % 10;
    if (eleventhDigit !== digits[10]) return false;
  
    // If all checks pass, the TC number is valid
    return true;
  };

  export const isValidPhoneNumber = (phoneNumber) => {
    // Ensure the input is a string
    if (typeof phoneNumber !== 'string') {
      return false;
    }
  
    // Check if the string is exactly 11 digits, starts with '0', and contains only digits
    const phoneRegex = /^0\d{10}$/;
  
    return phoneRegex.test(phoneNumber);
  };

  export const isValidEmail = (email) => {
    // Ensure input is a string
    if (typeof email !== 'string') return false;  
    // Regular expression to validate email
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    // Test the email against the regex
    return emailRegex.test(email);
  };
  
export const generateRandomPassword = () => {
  const upperCaseChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  const lowerCaseChars = "abcdefghijklmnopqrstuvwxyz";
  const digits = "0123456789";
  const specialChars = "!&*_,.";
  const allChars = upperCaseChars + lowerCaseChars + digits + specialChars;

  const getRandomChar = (chars) => chars[Math.floor(Math.random() * chars.length)];

  // Ensure at least one character from each required set
  const passwordArray = [
    getRandomChar(upperCaseChars), // At least one uppercase letter
    getRandomChar(lowerCaseChars), // At least one lowercase letter
    getRandomChar(digits),         // At least one digit
    getRandomChar(specialChars)    // At least one special character
  ];

  // Fill the rest of the password with random characters to make it 8 characters long
  while (passwordArray.length < 8) {
    passwordArray.push(getRandomChar(allChars));
  }

  // Shuffle the array to randomize character order
  for (let i = passwordArray.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [passwordArray[i], passwordArray[j]] = [passwordArray[j], passwordArray[i]];
  }

  // Return the password as a string
  return passwordArray.join('');
};
