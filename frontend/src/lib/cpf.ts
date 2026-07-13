export function onlyDigits(value: string): string {
  return value.replace(/\D/g, '')
}

function calcCheckDigit(digits: number[], length: number): number {
  let sum = 0
  for (let i = 0; i < length; i++) {
    sum += digits[i] * (length + 1 - i)
  }
  const remainder = (sum * 10) % 11
  return remainder === 10 ? 0 : remainder
}

export function isValidCPF(rawValue: string): boolean {
  const cpf = onlyDigits(rawValue)

  if (cpf.length !== 11 || /^(\d)\1{10}$/.test(cpf)) {
    return false
  }

  const digits = cpf.split('').map(Number)
  const firstCheckDigit = calcCheckDigit(digits, 9)
  const secondCheckDigit = calcCheckDigit(digits, 10)

  return firstCheckDigit === digits[9] && secondCheckDigit === digits[10]
}
