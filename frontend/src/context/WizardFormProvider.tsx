import { useEffect, type ReactNode } from 'react'
import { FormProvider, useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { defaultFormValues, formSchema, type FormValues } from '../schemas/formSchema'
import { loadStoredFormValues, persistFormValues } from '../lib/storage'

export function WizardFormProvider({ children }: { children: ReactNode }) {
  const methods = useForm<FormValues>({
    resolver: zodResolver(formSchema),
    mode: 'onSubmit',
    reValidateMode: 'onChange',
    defaultValues: {
      ...defaultFormValues,
      ...loadStoredFormValues<FormValues>(),
    },
  })

  useEffect(() => {
    const subscription = methods.watch((values) => {
      persistFormValues(values)
    })
    return () => subscription.unsubscribe()
  }, [methods])

  return <FormProvider {...methods}>{children}</FormProvider>
}
