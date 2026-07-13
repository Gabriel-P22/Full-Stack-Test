import { useNavigate } from 'react-router-dom'
import { useFormContext } from 'react-hook-form'
import type { FormValues } from '../schemas/formSchema'

export function useStepNavigation(fields: (keyof FormValues)[]) {
  const { trigger } = useFormContext<FormValues>()
  const navigate = useNavigate()

  async function goNext(path: string) {
    const isValid = await trigger(fields)
    if (isValid) {
      navigate(path)
    }
  }

  function goBack(path: string) {
    navigate(path)
  }

  return { goNext, goBack }
}
