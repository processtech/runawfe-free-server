const pad = (n: number): string => n.toString().padStart(2, '0');

/* formats as 'dd.mm.yyyy hh:mm' */
export function formatDateTime(date: Date): string {
  return formatDate(date) + formatTime(date);
}

/* formats as 'dd.mm.yyyy' */
export function formatDate(date: Date): string {
  return `${pad(date.getDate())}.${pad(date.getMonth() + 1)}.${date.getFullYear()} `
}

/* formats as 'hh:mm' */
export function formatTime(date: Date): string {
  return `${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

export function toBase64(file: File): Promise<{[fileName: string]: string}> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.readAsBinaryString(file);
    reader.onload = () => resolve({ [file.name]: btoa(reader.result as string) });
    reader.onerror = reject;
  });
}

export function truncateLabel(label: string, limit: number = 20): string {
  if (label.length <= limit) {
    return label;
  }
  return label.slice(0, limit / 2 - 3)
    + '...'
    + label.slice(label.length - limit / 2);
}

export const SIZE_100MB = 1024 * 1024 * 100;

export function extractContentFormat(format: string): string {
  return format.match(/\((.*)\)/)?.pop() || '';
}

export function currentTimeZone(): string {
  const offset = Math.ceil(new Date().getTimezoneOffset() / 60);
  const sign = (offset > 0) ? '-' : '+';
  return `${sign}${pad(Math.abs(offset))}:00`
}

export function removeListElementFormatIfPresent(format: string) {
  return format.replace(/\(.*\)$/, '')
}

// from yyyy.MM.dd hh:mm to yyyy-MM-ddThh:mm
export function datePickerFormatToIso(val: string): string {
  if (!val) {
    return '';
  }
  const [date, time] =  val.split(' ');
  const [day, month, year] = date.split('.');
  return `${year}-${month}-${day}T${time}`
}


// from yyyy-MM-ddThh:mm to yyyy.MM.dd hh:mm
export function dateIsoToDatePickerFormat(val: string): string {
  if (!val) {
    return ''
  }
  const [date, time] = val.split('T')
  const [year, month, day] = date.split('-')
  return `${day}.${month}.${year} ${time}`
}

export function escapeFilterString(str: string): string {
  if (!str) {
    return ''
  }
  return `*${str}*/i`
}

export function unescapeFilterString(str: string): string {
  if (!str) {
    return ''
  }
  return str.replace('*', '').replace('*/i', '')
}
