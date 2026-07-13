export function onlyDigits(value: string): string {
  return value.replace(/\D/g, '')
}

export function isValidBirthDate(value: string): boolean {
  const match = /^(\d{2})\/(\d{2})\/(\d{4})$/.exec(value)
  if (!match) return false

  const day = Number(match[1])
  const month = Number(match[2])
  const year = Number(match[3])
  const date = new Date(year, month - 1, day)

  const isRealCalendarDate =
    date.getFullYear() === year &&
    date.getMonth() === month - 1 &&
    date.getDate() === day

  if (!isRealCalendarDate) return false

  const today = new Date()
  today.setHours(0, 0, 0, 0)

  return date <= today && year >= 1900
}

export function parseCurrencyToNumber(value: string): number {
  const normalized = value.replace(/[^\d,-]/g, '').replace(',', '.')
  const parsed = Number.parseFloat(normalized)
  return Number.isNaN(parsed) ? 0 : parsed
}
