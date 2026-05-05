import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import client from '../../api/client';
import type { Approval } from '../../api/types';

type StatusFilter = 'ALL' | 'PENDING' | 'APPROVED' | 'REJECTED';

const STATUS_LABELS: Record<StatusFilter, string> = {
  ALL: 'All',
  PENDING: 'Pending',
  APPROVED: 'Approved',
  REJECTED: 'Rejected',
};

export default function AdminVenueApprovalsPage() {
  const [approvals, setApprovals] = useState<Approval[]>([]);
  const [filter, setFilter] = useState<StatusFilter>('ALL');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    client.get('/admin/approvals/all')
      .then(res => setApprovals(res.data.data))
      .finally(() => setLoading(false));
  }, []);

  const filtered = filter === 'ALL' ? approvals : approvals.filter(a => a.status === filter);

  const handleApprove = async (id: number) => {
    try {
      await client.post(`/admin/approvals/${id}/approve`, { note: 'Approved' });
      setApprovals(prev =>
        prev.map(a => (a.id === id ? { ...a, status: 'APPROVED' as const, reviewNote: 'Approved' } : a))
      );
    } catch (err: any) {
      alert(err.response?.data?.message || 'Action failed');
    }
  };

  const handleReject = async (id: number) => {
    const note = prompt('Rejection reason:');
    if (note === null) return;
    try {
      await client.post(`/admin/approvals/${id}/reject`, { note });
      setApprovals(prev =>
        prev.map(a => (a.id === id ? { ...a, status: 'REJECTED' as const, reviewNote: note } : a))
      );
    } catch (err: any) {
      alert(err.response?.data?.message || 'Action failed');
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">
          <h1 className="text-xl font-bold">All Venue Approvals</h1>
          <Link to="/admin" className="text-blue-600 hover:underline">Dashboard</Link>
        </div>
      </header>

      <main className="max-w-5xl mx-auto px-4 py-6 space-y-4">
        {/* Filter Tabs */}
        <div className="flex gap-1 bg-white rounded-lg shadow-sm p-1 w-fit">
          {(['ALL', 'PENDING', 'APPROVED', 'REJECTED'] as StatusFilter[]).map(s => (
            <button
              key={s}
              onClick={() => setFilter(s)}
              className={`px-4 py-2 text-sm font-medium rounded-md transition ${
                filter === s
                  ? 'bg-blue-600 text-white'
                  : 'text-gray-600 hover:bg-gray-100'
              }`}
            >
              {STATUS_LABELS[s]}
              {s !== 'ALL' && (
                <span className="ml-1 text-xs opacity-75">
                  ({approvals.filter(a => a.status === s).length})
                </span>
              )}
            </button>
          ))}
        </div>

        {/* Table */}
        {loading ? (
          <p className="text-gray-400 text-center py-8">Loading...</p>
        ) : filtered.length === 0 ? (
          <div className="bg-white rounded-lg shadow-sm p-8 text-center text-gray-400 text-sm">
            No approvals match the selected filter
          </div>
        ) : (
          <div className="bg-white rounded-lg shadow-sm overflow-hidden">
            <table className="w-full">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">Venue</th>
                  <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">Submitted By</th>
                  <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">Status</th>
                  <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">Review Note</th>
                  <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">Date</th>
                  <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {filtered.map(a => (
                  <tr key={a.id}>
                    <td className="px-4 py-3 text-sm font-medium">{a.venueName}</td>
                    <td className="px-4 py-3 text-sm text-gray-600">{a.submittedByName}</td>
                    <td className="px-4 py-3 text-sm">
                      <span className={`font-medium px-2 py-0.5 rounded text-xs ${
                        a.status === 'PENDING' ? 'bg-yellow-100 text-yellow-700' :
                        a.status === 'APPROVED' ? 'bg-green-100 text-green-700' :
                        'bg-red-100 text-red-700'
                      }`}>
                        {a.status}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-sm text-gray-600 max-w-[200px] truncate">
                      {a.reviewNote || '—'}
                    </td>
                    <td className="px-4 py-3 text-sm text-gray-600">
                      {new Date(a.createdAt).toLocaleDateString()}
                    </td>
                    <td className="px-4 py-3 text-sm">
                      {a.status === 'PENDING' && (
                        <div className="flex gap-2">
                          <button
                            onClick={() => handleApprove(a.id)}
                            className="px-3 py-1 bg-green-600 text-white rounded text-xs hover:bg-green-700"
                          >
                            Approve
                          </button>
                          <button
                            onClick={() => handleReject(a.id)}
                            className="px-3 py-1 bg-red-600 text-white rounded text-xs hover:bg-red-700"
                          >
                            Reject
                          </button>
                        </div>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </main>
    </div>
  );
}
