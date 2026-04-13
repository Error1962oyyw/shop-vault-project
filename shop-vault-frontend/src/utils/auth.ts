interface LoginErrorResult {
  errorMessage: string
  passwordError: string
}

export function parseLoginError(error: any): LoginErrorResult {
  const rawMsg = error?.response?.data?.msg || error?.message || ''
  const status = error?.response?.status
  const result: LoginErrorResult = { errorMessage: '', passwordError: '' }

  if (rawMsg.includes('账户已被暂停') || rawMsg.includes('已被暂停') || rawMsg.includes('Disabled') || rawMsg.includes('禁用')) {
    result.errorMessage = '您的账户已被暂停'
  } else if (rawMsg.includes('邮箱或密码错误') || rawMsg.includes('密码错误') || rawMsg.includes('密码不正确') || rawMsg.includes('invalid credentials') || rawMsg.includes('Bad credentials')) {
    result.passwordError = '邮箱或密码错误，请检查后重试'
  } else if (rawMsg.includes('邮箱不存在') || rawMsg.includes('用户不存在') || rawMsg.includes('not found') || rawMsg.includes('不存在') || rawMsg.includes('未注册')) {
    result.passwordError = '邮箱或密码错误，请检查后重试'
  } else if (rawMsg.includes('权限不足') || rawMsg.includes('Permission denied') || rawMsg.includes('Not admin')) {
    result.errorMessage = '该账号没有管理员权限'
  } else if (rawMsg && !rawMsg.includes('Request failed') && !rawMsg.includes('status code')) {
    result.errorMessage = rawMsg
  } else if (status === 403) {
    result.errorMessage = '您的账户已被暂停'
  } else if (status === 401) {
    result.passwordError = '邮箱或密码错误，请检查后重试'
  } else {
    result.errorMessage = '登录失败，请重试'
  }

  return result
}
