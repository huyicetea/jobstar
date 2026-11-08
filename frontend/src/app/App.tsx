import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { BrowserRouter, Route, Routes } from 'react-router-dom'
import { JobsPage } from '@/features/jobs/JobsPage'

const queryClient = new QueryClient()

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<JobsPage />} />
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  )
}
