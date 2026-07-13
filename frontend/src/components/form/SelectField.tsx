import { useFormContext } from 'react-hook-form'
import type { FormValues } from '../../schemas/formSchema'
import type { SelectOption } from '../../constants/options'

interface SelectFieldProps {
  name: keyof FormValues
  label: string
  options: SelectOption[]
  placeholder?: string
  disabled?: boolean
  fullWidth?: boolean
}

export function SelectField({ name, label, options, placeholder, disabled, fullWidth }: Readonly<SelectFieldProps>) {
  const {
    register,
    formState: { errors },
  } = useFormContext<FormValues>()
  const error = errors[name]?.message as string | undefined

  return (
    <div className={`field${fullWidth ? ' field-full' : ''}`}>
      <label htmlFor={name}>{label}</label>
      <select
        id={name}
        disabled={disabled}
        aria-invalid={!!error}
        aria-describedby={error ? `${name}-error` : undefined}
        defaultValue=""
        {...register(name)}
      >
        <option value="" disabled>
          {placeholder ?? 'Selecione...'}
        </option>
        {options.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
      {error && (
        <span className="field-error" id={`${name}-error`} role="alert">
          {error}
        </span>
      )}
    </div>
  )
}
