export function calculateValueDate(tradeDate: string, valueDate: string): string {
    const tradeDateObj = new Date(tradeDate);

    const match = valueDate.match(/T\+(\d+)/);
    if (match) {
        const daysToAdd = parseInt(match[1], 10);
        tradeDateObj.setDate(tradeDateObj.getDate() + daysToAdd);

        const year = tradeDateObj.getFullYear();
        const month = String(tradeDateObj.getMonth() + 1).padStart(2, "0");
        const day = String(tradeDateObj.getDate()).padStart(2, "0");

        return `${year}-${month}-${day}`;
    } else {
        console.warn("valueDate is not in T+ format.");
        return valueDate;
    }
}