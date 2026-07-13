import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useFormContext } from 'react-hook-form'
import type { FormValues } from '../schemas/formSchema'

export function useRequireStepData(requiredField: keyof FormValues, redirectTo: string) {
  const { getValues } = useFormContext<FormValues>()
  const navigate = useNavigate()

  useEffect(() => {
    if (!getValues(requiredField)) {
      navigate(redirectTo, { replace: true })
    }
  }, [])
}
