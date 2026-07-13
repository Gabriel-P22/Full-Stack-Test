import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useFormContext } from 'react-hook-form'
import type { FormValues } from '../schemas/formSchema'

/**
 * Guards a step against direct URL access before its prerequisite data exists,
 * e.g. opening /resumo without having gone through the earlier steps.
 */
export function useRequireStepData(requiredField: keyof FormValues, redirectTo: string) {
  const { getValues } = useFormContext<FormValues>()
  const navigate = useNavigate()

  useEffect(() => {
    if (!getValues(requiredField)) {
      navigate(redirectTo, { replace: true })
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])
}
