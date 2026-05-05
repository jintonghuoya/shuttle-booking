import { useState, useEffect } from 'react';
import client from '../../api/client';
import type { Approval } from '../../api/types';

type FilterStatus = 'ALL' | 'PENDING' | 'APPROVED' | 'REJECTED';

export default function AdminOrgApprovalsPage() {
  const [approvals, setApprovals] = useState<Approval[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState<FilterStatus>('ALL');

  const fetchApprovals = () => {
    setLoading(true);
    client.get('/admin/org-approvals')
      .then(res => setApprovals(res.data.data))
      .catch(() => alert('Failed to load approval requests'))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchApprovals();
  }, []);

  const filtered = filter === 'ALL'
    ? approvals
    : approvals.filter(a => a.status === filter);

  const handleAction = async (id: number, action: 'approve' | 'reject') => {
    try {
      await client.post(`/admin/org-approvals/${id}/${action}`);
      setApprovals(prev => prev.map(a =>
        a.id === id ? { ...a, status: action === 'approve' ? 'APPROVED' : 'REJECTED' } : a
      ));
    } catch (err: any) {
      alert(err.response?.data?.message || 'Action failed');
    }
  };

  const tabs: { label: string; value: FilterStatus }[] = [
    { label: 'All', value: 'ALL' },
    { label: 'Pending', value: 'PENDING' },
    { label: 'Approved', value: 'APPROVED' },
    { label: 'Rejected', value: 'REJECTED' },
  ];

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 py-4">
          <h1 className="text-2xl font-bold">Organization Approvals</h1>
        </div>
      </header>

      <main className="max-w-3xl mx-auto px-4 py-6">
        {/* Filter tabs */}
        <div className="flex gap-2 mb-4">
          {tabs.map(tab => (
            <button
              key={tab.value}
              onClick={() => setFilter(tab.value)}
              className={`px-4 py-2 rounded-lg text-sm font-medium transition ${
                filter === tab.value
                  ? 'bg-blue-600 text-white'
                  : 'bg-white border border-gray-200 hover:border-blue-300'
              }`}
            >
              {tab.label}
              {tab.value === 'PENDING' && (
                <span className="ml-1 text-xs">({approvals.filter(a => a.status === 'PENDING').length})</span>
              )}
            </button>
          ))}
        </div>

        {loading ? (
          <p className="text-gray-400 text-center">Loading...</p>
        ) : filtered.length === 0 ? (
          <p className="text-gray-400 text-center">No approval requests found.</p>
        ) : (
          <div className="space-y-3">
            {filtered.map(approval => (
              <div key={approval.id} className="bg-white rounded-lg shadow-sm p-4 border border-gray-200">
                <div className="flex justify-between items-start">
                  <div>
                    <h3 className="font-medium">{approval.venueName}</h3>
                    <p className="text-sm text-gray-500">Submitted by {approval.submittedByName}</p>
                    {approval.reviewNote && (
                      <p className="text-sm text-gray-500 mt-1">Note: {approval.reviewNote}</p>
                    )}
                    <p className="text-xs text-gray-400 mt-1">{approval.createdAt}</p>
                  </div>
                  <div className="text-right">
                    <span className={`text-xs px-2 py-1 rounded font-medium ${
                      approval.status === 'PENDING' ? 'bg-yellow-100 text-yellow-800'
                        : approval.status === 'APPROVED' ? 'bg-green-100 text-green-800'
                        : 'bg-red-100 text-red-800'
                    }`}>
                      {approval.status}
                    </span>
                  </div>
                </div>
                {approval.status === 'PENDING' && (
                  <div className="mt-3 flex gap-2">
                    <button
                      onClick={() => handleAction(approval.id, 'approve')}
                      className="bg-green-600 text-white px-4 py-2 rounded-lg text-sm hover:bg-green-700 transition"
                    >
                      Approve
                    </button>
                    <button
                      onClick={() => handleAction(approval.id, 'reject')}
                      className="bg-red-600 text-white px-4 py-2 rounded-lg text-sm hover:bg-red-700 transition"
                    >
                      Reject
                    </button>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </main>
    </div>
  );
}
