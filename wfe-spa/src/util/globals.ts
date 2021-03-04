const EN_LOCALE_ONLY = 'true'; // process.env.EN_LOCALE_ONLY === 'true'
const IN_BROWSER = typeof window !== 'undefined'
const IS_DEBUG = true; //process.env.DEBUG === 'true'
const IS_PROD = false; //process.env.NODE_ENV === 'production'

export default {
  EN_LOCALE_ONLY,
  IN_BROWSER,
  IS_DEBUG,
  IS_PROD,
}
