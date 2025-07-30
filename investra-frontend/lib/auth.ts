/**
 * Validates whether a given string is a properly formatted email address.
 *
 * This function uses a regular expression to check that the input:
 * - Contains exactly one "@" symbol,
 * - Has no whitespace characters,
 * - Has at least one character before and after the "@" symbol,
 * - Contains a period (".") after the "@" with at least one character on each side.
 *
 * @param {string} email - The email address string to validate.
 * @returns {boolean} - Returns true if the email matches the expected format, false otherwise.
 */
export const validateEmail = (email: string) => {
    const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return regex.test(email);
};