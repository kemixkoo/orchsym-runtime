const company = 'Baishan Cloud'
const companyLink = 'https://www.baishancloud.com/zh/'
const departmentName = 'Orchsym Studio'
const departmentLink = 'https://www.baishancloud.com/tech/orchsym/'
const version = 'v3.0.0'
const copyright = `2017-${new Date().getFullYear()} 白山云科技-数聚蜂巢`
const departmentNameEn = 'Orchsym Studio'
const indexLogoPath = '/iconfont/logo.svg'
const indexMiniLogoPath = '/iconfont/logo-mini.svg'
const faviconPath = '/favicon.ico'
// 可配置企业logo
window.logoHref = {
  companyLogoIndex: indexLogoPath,
  companyLogoIndexMix: indexMiniLogoPath,
  companyIco: faviconPath,
}
// title配置
window.departmentNameEn = departmentNameEn

// footer信息配置
window.version = version
window.copyright = `${copyright}`
window.globalFooterInfo = [{
  key: 'company',
  title: company,
  href: companyLink,
  blankTarget: true,
}, {
  key: 'department',
  title: departmentName,
  href: departmentLink,
  blankTarget: true,
}, {
  key: 'version',
  title: version || '',
}]
