/*
 * Created by Ranorex
 * User: ffg9ym
 * Date: 22-09-2020
 * Time: 13:07
 * 
 * To change this template use Tools > Options > Coding > Edit standard headers.
 */
using System;
using System.Collections.Generic;
using System.Text;
using System.Text.RegularExpressions;
using System.Drawing;
using System.Threading;
using WinForms = System.Windows.Forms;

using Ranorex;
using Ranorex.Core;
using Ranorex.Core.Testing;

namespace SolutionProject.TC_Common
{
    /// <summary>
    /// Description of Third.
    /// </summary>
    [TestModule("F88EB0C4-0BFE-4094-9D17-71BBA75016FD", ModuleType.UserCode, 1)]
    public class Third : ITestModule
    {
        /// <summary>
        /// Constructs a new instance.
        /// </summary>
        public Third()
        {
            // Do not delete - a parameterless constructor is required!
        }

        /// <summary>
        /// Performs the playback of actions in this module.
        /// </summary>
        /// <remarks>You should not call this method directly, instead pass the module
        /// instance to the <see cref="TestModuleRunner.Run(ITestModule)"/> method
        /// that will in turn invoke this method.</remarks>
        void ITestModule.Run()
        {
            Report.Success("third");
        }
    }
}
