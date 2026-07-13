import type { ReactElement, ReactNode } from 'react'
import { render } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { FormProvider, useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { defaultFormValues, formSchema, type FormValues } from '../schemas/formSchema'

function FormWrapper({
  children,
  initialValues,
}: {
  children: ReactNode
  initialValues?: Partial<FormValues>
}) {
  const methods = useForm<FormValues>({
    resolver: zodResolver(formSchema),
    defaultValues: { ...defaultFormValues, ...initialValues },
  })

  return <FormProvider {...methods}>{children}</FormProvider>
}

interface RenderWithFormOptions {
  initialValues?: Partial<FormValues>
  initialPath?: string
}

export function renderWithForm(ui: ReactElement, options: RenderWithFormOptions = {}) {
  const { initialValues, initialPath = '/' } = options

  return render(
    <MemoryRouter initialEntries={[initialPath]}>
      <FormWrapper initialValues={initialValues}>{ui}</FormWrapper>
    </MemoryRouter>,
  )
}
