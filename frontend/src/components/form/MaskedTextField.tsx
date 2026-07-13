import { Controller, useFormContext } from 'react-hook-form'
import { IMaskInput } from 'react-imask'
import type { FormValues } from '../../schemas/formSchema'

interface MaskedTextFieldProps {
  name: keyof FormValues
  label: string
  maskOptions: Record<string, unknown>
  placeholder?: string
  inputMode?: 'text' | 'numeric' | 'decimal'
  hint?: string
  fullWidth?: boolean
}

export function MaskedTextField({
  name,
  label,
  maskOptions,
  placeholder,
  inputMode,
  hint,
  fullWidth,
}: Readonly<MaskedTextFieldProps>) {
  const {
    control,
    formState: { errors },
  } = useFormContext<FormValues>()
  const error = errors[name]?.message as string | undefined

  return (
    <div className={`field${fullWidth ? ' field-full' : ''}`}>
      <label htmlFor={name}>{label}</label>
      <Controller
        name={name}
        control={control}
        render={({ field }) => (
          <IMaskInput
            {...maskOptions}
            id={name}
            name={field.name}
            value={field.value ?? ''}
            unmask={false}
            inputMode={inputMode}
            placeholder={placeholder}
            aria-invalid={!!error}
            aria-describedby={error ? `${name}-error` : undefined}
            onAccept={(value: string) => field.onChange(value)}
            onBlur={field.onBlur}
          />
        )}
      />
      {error && (
        <span className="field-error" id={`${name}-error`} role="alert">
          {error}
        </span>
      )}
      {!error && hint && <span className="field-hint">{hint}</span>}
    </div>
  )
}
