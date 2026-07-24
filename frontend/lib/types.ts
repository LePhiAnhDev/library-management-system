// Response shapes mirroring the backend DTOs. Money values arrive as numbers; dates as ISO strings.

export interface Page<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  sort: string
}

export type RecordStatus = "ACTIVE" | "INACTIVE"

export interface UserProfile {
  id: number
  clerkUserId: string
  email: string | null
  fullName: string | null
  avatarUrl: string | null
  status: string
  createdAt: string
}

export interface Category {
  id: number
  name: string
  description: string | null
  parentId: number | null
  parentName: string | null
  status: RecordStatus
  createdAt: string
  updatedAt: string
}

export interface Author {
  id: number
  fullName: string
  biography: string | null
  status: RecordStatus
  createdAt: string
  updatedAt: string
}

export interface Publisher {
  id: number
  name: string
  address: string | null
  phone: string | null
  email: string | null
  status: RecordStatus
  createdAt: string
  updatedAt: string
}

export interface AuthorSummary {
  id: number
  fullName: string
}

export interface Book {
  id: number
  isbn: string
  title: string
  subtitle: string | null
  description: string | null
  publisherId: number | null
  publisherName: string | null
  categoryId: number | null
  categoryName: string | null
  authors: AuthorSummary[]
  publicationYear: number | null
  language: string | null
  pageCount: number | null
  coverImageUrl: string | null
  totalCopies: number
  availableCopies: number
  status: RecordStatus
  createdAt: string
  updatedAt: string
}

export type BookCopyStatus =
  | "AVAILABLE"
  | "BORROWED"
  | "RESERVED"
  | "LOST"
  | "DAMAGED"
  | "MAINTENANCE"

export interface BookCopy {
  id: number
  bookId: number
  bookTitle: string
  barcode: string
  shelfLocation: string | null
  status: BookCopyStatus
  acquiredDate: string | null
  conditionNote: string | null
  createdAt: string
  updatedAt: string
}

export type MembershipType = "REGULAR" | "STUDENT" | "PREMIUM"
export type MemberStatus = "ACTIVE" | "SUSPENDED" | "EXPIRED"

export interface Member {
  id: number
  memberCode: string
  fullName: string
  email: string
  phone: string | null
  address: string | null
  membershipType: MembershipType
  joinDate: string
  expiryDate: string | null
  status: MemberStatus
  createdAt: string
  updatedAt: string
}

export type LoanStatus = "BORROWED" | "RETURNED" | "OVERDUE"

export interface Loan {
  id: number
  code: string
  memberId: number
  memberCode: string
  memberName: string
  bookCopyId: number
  barcode: string
  bookId: number
  bookTitle: string
  borrowDate: string
  dueDate: string
  returnDate: string | null
  status: LoanStatus
  overdue: boolean
  renewCount: number
  createdById: number | null
  createdByName: string | null
  returnedById: number | null
  returnedByName: string | null
  createdAt: string
}

export type ReservationStatus = "PENDING" | "READY" | "FULFILLED" | "CANCELLED" | "EXPIRED"

export interface Reservation {
  id: number
  memberId: number
  memberCode: string
  memberName: string
  bookId: number
  bookTitle: string
  status: ReservationStatus
  reservationDate: string
  readyAt: string | null
  pickupExpiry: string | null
  heldCopyId: number | null
  heldCopyBarcode: string | null
}

export type FineType = "OVERDUE" | "LOST" | "DAMAGED"
export type FineStatus = "UNPAID" | "PAID" | "WAIVED"

export interface Fine {
  id: number
  memberId: number
  memberCode: string
  memberName: string
  loanId: number | null
  loanCode: string | null
  type: FineType
  amount: number
  status: FineStatus
  reason: string | null
  paidAt: string | null
  settledById: number | null
  settledByName: string | null
  createdAt: string
}

export interface LoanPolicy {
  membershipType: MembershipType
  maxBooks: number
  loanPeriodDays: number
  maxRenewals: number
}

export interface Settings {
  libraryName: string
  libraryAddress: string | null
  overdueFinePerDay: number
  fineBlockThreshold: number
  reservationHoldDays: number
  lostDefaultFee: number
  damagedDefaultFee: number
  loanPolicies: LoanPolicy[]
  updatedAt: string
}

export interface MemberProfile {
  member: Member
  currentLoans: Loan[]
  unpaidFines: Fine[]
  totalUnpaidFines: number
  activeReservations: Reservation[]
}

export interface DashboardStats {
  totalBooks: number
  totalCopies: number
  totalMembers: number
  borrowedCount: number
  overdueCount: number
  pendingReservations: number
  finesCollectedThisMonth: number
}

export interface TopBook {
  bookId: number
  title: string
  borrowCount: number
}

export interface ActiveMember {
  memberId: number
  memberCode: string
  fullName: string
  loanCount: number
}

export interface LoanTrendPoint {
  date: string
  count: number
}

export interface InventoryRow {
  status: BookCopyStatus
  count: number
}

export interface FinesSummary {
  collected: number
  waived: number
  unpaidTotal: number
}
