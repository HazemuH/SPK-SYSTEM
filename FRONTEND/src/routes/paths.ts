/** Central route paths. Never hardcode route strings elsewhere — import from here. */
export const paths = {
  login: "/login",
  dashboard: "/",
  toys: "/toys",
  categories: "/categories",
  criteria: "/criteria",
  weightProfiles: "/weight-profiles",
  pairwise: "/pairwise",
  calculation: "/calculation",
  results: "/results",
  reports: "/reports",
  settings: "/settings",
} as const;
