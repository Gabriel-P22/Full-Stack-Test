import axios from 'axios'

export const mockApi = axios.create({
  baseURL: import.meta.env.VITE_MOCK_API_URL ?? 'http://localhost:3001',
})

export interface CepResult {
  endereco: string
  bairro: string
  cidade: string
  estado: string
}

interface ViaCepResponse {
  logradouro: string
  bairro: string
  localidade: string
  uf: string
  erro?: boolean
}

async function fetchCepFromMock(cep: string): Promise<CepResult | null> {
  try {
    const { data } = await mockApi.get<CepResult>(`/ceps/${cep}`)
    return data
  } catch {
    return null
  }
}

async function fetchCepFromViaCep(cep: string): Promise<CepResult | null> {
  try {
    const { data } = await axios.get<ViaCepResponse>(`https://viacep.com.br/ws/${cep}/json/`)
    if (data.erro) return null
    return {
      endereco: data.logradouro,
      bairro: data.bairro,
      cidade: data.localidade,
      estado: data.uf,
    }
  } catch {
    return null
  }
}

export async function fetchAddressByCep(cep: string): Promise<CepResult | null> {
  const mockResult = await fetchCepFromMock(cep)
  if (mockResult) return mockResult
  return fetchCepFromViaCep(cep)
}

export interface Profession {
  id: number | string
  name: string
}

export async function fetchProfessions(): Promise<Profession[]> {
  const { data } = await mockApi.get<Profession[]>('/professions')
  return data
}
