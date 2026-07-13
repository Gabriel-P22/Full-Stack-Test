import { useFormContext } from 'react-hook-form'
import type { FormValues } from '../../schemas/formSchema'

interface TextFieldProps {
  name: keyof FormValues
  label: string
  placeholder?: string
  type?: string
  autoComplete?: string
  fullWidth?: boolean
  readOnly?: boolean
}

export function TextField({
  name,
  label,
  placeholder,
  type = 'text',
  autoComplete,
  fullWidth,
  readOnly,
}: Readonly<TextFieldProps>) {
  const {
    register,
    formState: { errors },
  } = useFormContext<FormValues>()
  const error = errors[name]?.message as string | undefined

  return (
    <div className={`field${fullWidth ? ' field-full' : ''}`}>
      <label htmlFor={name}>{label}</label>
      <input
        id={name}
        type={type}
        placeholder={placeholder}
        autoComplete={autoComplete}
        readOnly={readOnly}
        aria-invalid={!!error}
        aria-describedby={error ? `${name}-error` : undefined}
        {...register(name)}
      />
      {error && (
        <span className="field-error" id={`${name}-error`} role="alert">
          {error}
        </span>
      )}
    </div>
  )
}
