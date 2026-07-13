import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { Layout } from './components/layout/Layout'
import { WizardFormProvider } from './context/WizardFormProvider'
import { Step1PersonalData } from './pages/Step1PersonalData'
import { Step2Address } from './pages/Step2Address'
import { Step3Professional } from './pages/Step3Professional'
import { SummaryPage } from './pages/SummaryPage'

function App() {
  return (
    <BrowserRouter>
      <WizardFormProvider>
        <Layout>
          <Routes>
            <Route path="/" element={<Navigate to="/etapa-1" replace />} />
            <Route path="/etapa-1" element={<Step1PersonalData />} />
            <Route path="/etapa-2" element={<Step2Address />} />
            <Route path="/etapa-3" element={<Step3Professional />} />
            <Route path="/resumo" element={<SummaryPage />} />
            <Route path="*" element={<Navigate to="/etapa-1" replace />} />
          </Routes>
        </Layout>
      </WizardFormProvider>
    </BrowserRouter>
  )
}

export default App
