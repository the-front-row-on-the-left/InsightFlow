export type WrappedApiResponse<T> = {
  success: boolean
  data: T
  meta?: {
    request_id: string
  }
}

async function parseJson<T>(response: Response): Promise<T> {
  if (!response.ok) {
    throw new Error(`Request failed with status ${response.status}`)
  }
  return (await response.json()) as T
}

export async function fetchWrapped<T>(baseUrl: string, path: string): Promise<T> {
  const payload = await parseJson<WrappedApiResponse<T>>(
    await fetch(`${baseUrl}${path}`, {
      headers: {
        Accept: 'application/json',
      },
    }),
  )

  if ('success' in payload && !payload.success) {
    throw new Error('API returned an unsuccessful response.')
  }

  return payload.data
}

export async function fetchPlain<T>(baseUrl: string, path: string): Promise<T> {
  return parseJson<T>(
    await fetch(`${baseUrl}${path}`, {
      headers: {
        Accept: 'application/json',
      },
    }),
  )
}
