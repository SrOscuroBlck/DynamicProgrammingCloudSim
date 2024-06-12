package org.example;

import org.cloudsimplus.brokers.DatacenterBrokerSimple;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.vms.Vm;

import java.util.ArrayList;
import java.util.List;

public class DynamicProgramming {

    public static List<Cloudlet> optimizeResourceAllocation(List<Cloudlet> tasks, List<Vm> vms, DatacenterBrokerSimple broker) {
        int n = tasks.size();
        int m = vms.size();
        double[][] dp = new double[n + 1][m + 1];
        int[][] allocation = new int[n + 1][m + 1];

        // Initialize DP and allocation tables
        for (int i = 0; i <= n; i++) {
            for (int j = 0; j <= m; j++) {
                dp[i][j] = Double.MAX_VALUE;
                allocation[i][j] = -1;
            }
        }
        dp[0][0] = 0;

        // Fill DP table
        for (int i = 1; i <= n; i++) {
            for (int k = 1; k <= m; k++) {
                Vm vm = vms.get(k - 1);
                for (int j = 1; j <= i; j++) {
                    double currentCost = dp[i - j][k - 1] + calculateCost(tasks.subList(i - j, i), vm);
                    if (currentCost < dp[i][k]) {
                        dp[i][k] = currentCost;
                        allocation[i][k] = j;
                    }
                }
            }
        }

        // Traceback to find the optimal allocation
        List<Cloudlet> assignedTasks = new ArrayList<>();
        int i = n;
        int k = m;
        while (i > 0 && k > 0) {
            int j = allocation[i][k];
            if (j > 0) {
                for (int t = 0; t < j; t++) {
                    Cloudlet task = tasks.get(i - t - 1);
                    Vm vm = vms.get(k - 1);
                    if (canAllocate(task, vm)) {
                        task.setVm(vm);
//                        broker.submitCloudlet(task);
                        assignedTasks.add(task);
                    }
                }
            }
            i -= j;
            k--;
        }

        // Submit remaining tasks to VMs
        for (Cloudlet task : tasks) {
            if (!assignedTasks.contains(task)) {
                Vm vm = findBestVm(task, vms);
                if (vm != null) {
                    task.setVm(vm);
                    broker.submitCloudlet(task);
                    assignedTasks.add(task);
                }
            }
        }

        return assignedTasks;
    }

    private static boolean canAllocate(Cloudlet task, Vm vm) {
        double ramUsage = vm.getRam().getCapacity() * task.getUtilizationOfRam(0);
        double bwUsage = vm.getBw().getCapacity() * task.getUtilizationOfBw(0);
        return ramUsage <= vm.getRam().getCapacity() && bwUsage <= vm.getBw().getCapacity();
    }

    private static double calculateCost(List<Cloudlet> tasks, Vm vm) {
        double totalCost = 0.0;
        for (Cloudlet task : tasks) {
            double taskCost = task.getLength() / vm.getMips();
            totalCost += taskCost;
        }
        return totalCost;
    }

    private static Vm findBestVm(Cloudlet task, List<Vm> vms) {
        Vm bestVm = null;
        double bestVmCost = Double.MAX_VALUE;

        for (Vm vm : vms) {
            if (canAllocate(task, vm)) {
                double cost = calculateCost(List.of(task), vm);
                if (cost < bestVmCost) {
                    bestVmCost = cost;
                    bestVm = vm;
                }
            }
        }

        return bestVm;
    }
}
