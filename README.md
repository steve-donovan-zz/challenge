**Assumptions**
<li>A RIC will be more complex than just a String in reality.</li>
<li>A User will be more complex than just a String in reality.</li>
<li>Orders will be added by a single thread to maintain order of execution. If requirement is for multiplre threads, then a framework may be appropriate.</li>
<li>Querying of data sets could be via multiple threads.</li>
<li>Testing against 4 decimal places on price with HALF DOWN rounding.</li>

<br/>
<p>I understand there was no requirement for tests, but for my own benefit I created a single sweeping unit test.</p>
