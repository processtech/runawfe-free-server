export const systemConfiguration = new class SystemConfiguration {
  publicPath(): string {
    return  process.env.NODE_ENV === 'development' ? '' : '/spa'
  }

  serverUrl(): string {
    return process.env.NODE_ENV === 'development'
      ? 'http://localhost:8080'
      : window.location.origin
  }

  webSocketUrl(): string {
    const wsProtocol = (window.location.protocol === 'https:') ? 'wss:' : 'ws:'
    const host = (process.env.NODE_ENV === 'development')
      ? '127.0.0.1:8080'
      : window.location.host
    return `${wsProtocol}//${host}`
  }
}
