/**
 * Creates a URL-friendly path for a given page name.
 * Example: "Dashboard" -> "/dashboard"
 * @param {string} pageName - The name of the page.
 * @returns {string} The formatted URL path.
 */
export const createPageUrl = (pageName) => {
    // A router root can be added here if needed, e.g., /app/${pageName.toLowerCase()}
    return `/${pageName.toLowerCase()}`;
};