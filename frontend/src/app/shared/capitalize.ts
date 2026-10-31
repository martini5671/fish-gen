// Turns backend enum values into labels, e.g. ENGLISH -> English
export function capitalize(value: string): string {
  return value.charAt(0).toUpperCase() + value.slice(1).toLowerCase();
}
