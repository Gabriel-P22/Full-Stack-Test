import { beforeEach, describe, expect, it, vi } from 'vitest'

const { instanceGet, topLevelGet } = vi.hoisted(() => ({
  instanceGet: vi.fn(),
  topLevelGet: vi.fn(),
}))

vi.mock('axios', () => ({
  default: {
    create: vi.fn(() => ({ get: instanceGet })),
    get: topLevelGet,
  },
}))

import { fetchAddressByCep, fetchProfessions } from './api'

beforeEach(() => {
  instanceGet.mockReset()
  topLevelGet.mockReset()
})

describe('fetchAddressByCep', () => {
  it('returns the address from the mocked json-server when found', async () => {
    instanceGet.mockResolvedValueOnce({
      data: { endereco: 'Avenida Paulista', bairro: 'Bela Vista', cidade: 'São Paulo', estado: 'SP' },
    })

    const result = await fetchAddressByCep('01310100')

    expect(result).toEqual({ endereco: 'Avenida Paulista', bairro: 'Bela Vista', cidade: 'São Paulo', estado: 'SP' })
    expect(instanceGet).toHaveBeenCalledWith('/ceps/01310100')
    expect(topLevelGet).not.toHaveBeenCalled()
  })

  it('falls back to ViaCEP when the CEP is not in the mock db', async () => {
    instanceGet.mockRejectedValueOnce(new Error('Request failed with status code 404'))
    topLevelGet.mockResolvedValueOnce({
      data: { logradouro: 'Rua das Flores', bairro: 'Centro', localidade: 'Rio de Janeiro', uf: 'RJ' },
    })

    const result = await fetchAddressByCep('20000000')

    expect(result).toEqual({ endereco: 'Rua das Flores', bairro: 'Centro', cidade: 'Rio de Janeiro', estado: 'RJ' })
  })

  it('returns null when ViaCEP reports the CEP does not exist', async () => {
    instanceGet.mockRejectedValueOnce(new Error('Request failed with status code 404'))
    topLevelGet.mockResolvedValueOnce({ data: { erro: true } })

    const result = await fetchAddressByCep('00000000')

    expect(result).toBeNull()
  })

  it('returns null when both the mock and ViaCEP fail', async () => {
    instanceGet.mockRejectedValueOnce(new Error('network error'))
    topLevelGet.mockRejectedValueOnce(new Error('network error'))

    const result = await fetchAddressByCep('00000000')

    expect(result).toBeNull()
  })
})

describe('fetchProfessions', () => {
  it('returns the list of professions from the mocked API', async () => {
    instanceGet.mockResolvedValueOnce({ data: [{ id: 1, name: 'Engenheiro(a) Civil' }] })

    const result = await fetchProfessions()

    expect(result).toEqual([{ id: 1, name: 'Engenheiro(a) Civil' }])
    expect(instanceGet).toHaveBeenCalledWith('/professions')
  })
})
