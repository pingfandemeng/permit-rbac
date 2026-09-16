import JSEncrypt from 'jsencrypt'

export function encryptPassword(plain, publicKey) {
  if (!publicKey) {
    return plain
  }
  const encryptor = new JSEncrypt()
  encryptor.setPublicKey(publicKey)
  const cipher = encryptor.encrypt(plain)
  return cipher || plain
}
